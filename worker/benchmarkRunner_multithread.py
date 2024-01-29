import traceback
from exampleGenerator import *
from Scheduler import scheduleThresholdSearch
from Exceptions import TrainerConstraintExcpetion, IntensityConstraintException


from multiprocessing import JoinableQueue, Process
import time
import signal

import matplotlib.pyplot as plt

MAX_TIMEOUT = 60


def print_stats(success, fail):
    timeout_cnt = 0
    except_cnt = 0
    schedule_constraint_cnt = 0
    intensity_constraint_cnt = 0
    for res in fail:
        if res["reason"] == "T":
            timeout_cnt += 1
        if "E" in res["reason"]:
            except_cnt += 1
            if "I" in res["reason"]:
                intensity_constraint_cnt += 1
            if "S" in res["reason"]:
                schedule_constraint_cnt += 1
    print("=========================================================================")
    print(
        "solved tasks: {} ({}%)".format(
            len(success), int(len(success) / (len(fail)+1 + len(success)) * 100)
        )
    )
    print(
        "unsolved tasks: {} ({}%)".format(
            len(fail), int(len(fail) / (len(fail)+1 + len(success)) * 100)
        )
    )
    if timeout_cnt > 0:
        print(
            "unsolved due to timeout: {} ({}%)".format(
                timeout_cnt, int((timeout_cnt / (len(fail)+1)) * 100)
            )
        )
    if schedule_constraint_cnt > 0:
        print(
            "unsolved due to schedule constraint exception: {} ({}%)".format(
                schedule_constraint_cnt,
                int((schedule_constraint_cnt / except_cnt) * 100),
            )
        )
    if intensity_constraint_cnt > 0:
        print(
            "unsolved due to intensity constraint exception: {} ({}%)".format(
                intensity_constraint_cnt,
                int((intensity_constraint_cnt / except_cnt) * 100),
            )
        )
    if except_cnt - (intensity_constraint_cnt + schedule_constraint_cnt) > 0:
        print(
            "unsolved due to unhandeled exception: {} ({}%)".format(
                except_cnt - (intensity_constraint_cnt + schedule_constraint_cnt),
                int(
                    (
                        (
                            except_cnt
                            - (intensity_constraint_cnt + schedule_constraint_cnt)
                        )
                        / except_cnt
                    )
                    * 100
                ),
            )
        )
    print("=========================================================================")

    # Calculate minimum, maximum, and average
    minimum = min(success)
    maximum = max(success)
    average = sum(success) / len(success)

    _sorted = sorted(success)

    n = len(_sorted)
    q1_pos = (n + 1) // 4
    q2_pos = (2 * n + 1) // 4
    q3_pos = (3 * n + 1) // 4

    q1 = _sorted[q1_pos - 1]
    q2 = _sorted[q2_pos - 1]
    q3 = _sorted[q3_pos - 1]

    print(
        "Min:{}ms ,Max:{}ms ,Avg:{}ms".format(int(minimum), int(maximum), int(average))
    )

    print("Q1:{}ms ,Q2:{}ms ,Q3:{}ms".format(int(q1), int(q2), int(q3)))
    print("=========================================================================")

    plt.boxplot([success], labels=['Data'])
    plt.title('Boxplot of the successful requests')
    plt.ylabel("[ms]")
    plt.savefig('boxplot.png')


class TimerInterrupException(Exception):
    def __init__(self):
        super().__init__("")


def handler(signum, frame):
    raise TimerInterrupException()


# Define the worker function
def worker(worker_id, task_queue: JoinableQueue, result_queue, failed_queue):
    # print(f"worker {worker_id} started")
    while True:
        task = task_queue.get()
        if task is None:
            task_queue.task_done()
            # print("[Process {} terminating] ".format(worker_id), flush=True)
            break  # Exit the loop if a sentinel value is received

        (number, problem) = task
        (function, activities, schedule, stepWidth) = problem
        signal.signal(signal.SIGALRM, handler)
        signal.alarm(MAX_TIMEOUT)

        try:
            start_time = time.time()
            function(activities, schedule, stepWidth)
            end_time = time.time()
            elapsed_time = (end_time - start_time) * 1000  # Convert to milliseconds
            result_queue.put(elapsed_time)
        except TrainerConstraintExcpetion as e:
            failed_queue.put({"id":number,"reason":"ES"})
        except IntensityConstraintException as e:
            failed_queue.put(({"id":number,"reason":"EI"}))
        except TimerInterrupException:
            failed_queue.put(({"id":number,"reason":"T"}))
        except Exception as e:
            print("-------------")
            traceback.print_exc()
            failed_queue.put(({"id":number,"reason":"E"}))
        finally:
            task_queue.task_done()


# Define the task queue class
class TaskQueue:
    def __init__(self, num_workers):
        self.task_queue = JoinableQueue()
        self.result_queue = JoinableQueue()
        self.failed_queue = JoinableQueue()
        self.workers = []
        self.num_workers = num_workers

    def start_workers(self):
        for i in range(self.num_workers):
            p = Process(
                target=worker,
                args=(i, self.task_queue, self.result_queue, self.failed_queue),
            )
            p.daemon = True
            p.start()
            self.workers.append(p)

    def add_task(self, task):
        self.task_queue.put(task)

    def wait_completion(self):
        self.failed_queue.cancel_join_thread()
        self.result_queue.cancel_join_thread()
        # Add sentinel values to signal workers to exit
        for _ in range(self.num_workers):
            self.task_queue.put(None)

        self.task_queue.join()

    def get_results(self):
        success = []
        while not self.result_queue.empty():
            success.append(self.result_queue.get())

        fail = []
        while not self.failed_queue.empty():
            fail.append(self.failed_queue.get())
        return (success, fail)


def benchmarkRun(function, exampleCnt, stepWidth, additionalContext=""):
    start_time = time.time()
           
    # Example usage
    task_queue = TaskQueue(num_workers=8)

    # Start worker threads
    task_queue.start_workers()

    # Add tasks to the queue
    for i in range(0, exampleCnt-1):
        (activities, schedule) = loadExample(i)
        task_queue.add_task((i, (function, activities, schedule, stepWidth)))

    # Wait for all tasks to complete
    task_queue.wait_completion()

    (success, fail) = task_queue.get_results()

    end_time = time.time()
    elapsed_time = (end_time - start_time)
    print("=========================================================================")
    print("Run of {} examples with stepwidth of {}".format(exampleCnt, stepWidth))
    print("Running this benchmark took {}s".format(elapsed_time))
    if additionalContext != "":
        print(additionalContext)
    print_stats(success, fail)
    print("")


def main():
    benchmarkRun(scheduleThresholdSearch, 100, 1, "without hashSet")
    #benchmarkRun(scheduleThresholdSearch, 1000, 1, "without hashSet")


if __name__ == "__main__":
    main()
