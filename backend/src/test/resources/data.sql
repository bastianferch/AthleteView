---
-- #%L
-- athlete_view
-- %%
-- Copyright (C) 2023 - 2024 TU Wien INSO ASE GROUP 5 WS2023
-- %%
-- Permission is hereby granted, free of charge, to any person obtaining a copy
-- of this software and associated documentation files (the "Software"), to deal
-- in the Software without restriction, including without limitation the rights
-- to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
-- copies of the Software, and to permit persons to whom the Software is
-- furnished to do so, subject to the following conditions:
-- 
-- The above copyright notice and this permission notice shall be included in
-- all copies or substantial portions of the Software.
-- 
-- THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
-- IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
-- FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
-- AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
-- LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
-- OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
-- THE SOFTWARE.
-- #L%
---
INSERT INTO preferences (id, email_notifications, comment_notifications, rating_notifications, other_notifications, share_health_with_trainer)
VALUES (-1, FALSE, '1', '1', '1', FALSE),
       (-2, FALSE, '1', '1', '1', TRUE),
       (-3, FALSE, '1', '1', '1', FALSE),
       (-4, FALSE, '1', '1', '1', FALSE);

INSERT INTO users (id, name, password, country, email, zip, is_confirmed, preferences_id)
VALUES (-1, 'Josef', 'testpwd', 'Austria', 'a@b.com', '1337', TRUE, -1),
       (-2, 'Mustermann', 'testpwd', 'Bustria', 'b@a.com', '6942', TRUE, -2),
       (-3, 'Mustertrainer', 'testpwd', 'Germany', 'train@best.er', '4201', TRUE, -3),
       (-4, 'Real trainings user', 'testpwd', 'Austria', 'a@trainer.co', '1337', TRUE, -4);

INSERT INTO trainer (id, code)
VALUES (-3, '203');

INSERT INTO athlete (id, dob, height, weight)
VALUES (-1, PARSEDATETIME('02-03-1999', 'dd-MM-yyyy'), '189', '1000'),
       (-4, PARSEDATETIME('02-03-1989', 'dd-MM-yyyy'), '175', '700');

INSERT INTO athlete (id, dob, height, weight, trainer_id)
VALUES (-2, PARSEDATETIME('03-02-2001', 'dd-MM-yyyy'), '130', '1001', -3);


INSERT INTO step (id, duration_distance, duration_distance_unit, duration_type, target_from, target_to, target_type, type, note)
VALUES (-1, '1024', '1', '0', '10', '20', '0', '0', 'sample step #1'),
       (-2, '2048', '0', '2', '10', '20', '0', '2', 'sample step #2'),
       (-3, '4096', '2', '1', '10', '20', '0', '1', 'sample step #3'),
       (-4, '7', '3', '2', '100', '650', '2', '3', 'sample step #4'),
       (-5, '700', '2', '1', '99', '100', '1', '1', 'sample step #5'),
       (-6, '700', '2', '1', '99', '100', '1', '1', 'sample step #6'),
       (-7, null, null, '2', null, null, null, '2', 'lap button warm up #1'),
       (-8, '1', '0', '1', '240', '260', '2', '0', '1km 4:00-4:20'),
       (-9, '2', '2', '1', null, null, null, '1', '2min recovery'),
       (-10, null, null, '2', null, null, null, '3', 'lap button cooldown #1'),
       (-11, '60', '2', '1', '99', '100', '1', '1', 'sample step #11'),
       (-12, '60', '2', '1', '99', '100', '1', '1', 'sample step #12'),
       (-13, '700', '2', '1', '99', '100', '1', '1', 'sample step #13'),
       (-14, '700', '2', '1', '99', '100', '1', '1', 'sample step #14');


INSERT INTO activity_interval (id, repeat, step_id)
VALUES (-1, 1, -1),
       (-2, 2, -2),
       (-3, 5, -3),
       (-4, 1, -4),
       (-5, 2, -5),
       (-6, 1, -6),
       (-7, 1, -7),
       (-8, 1, -8),
       (-9, 1, -9),
       (-10, 7, null),
       (-11, 1, -10),
       (-12, 1, null),
       (-13, 1, -11),
       (-14, 1, -12),
       (-15, 1, -13),
       (-16, 1, -14);

INSERT INTO activity_interval_intervals(interval_id, intervals_id)
VALUES (-10, -8),
       (-10, -9),
       (-12, -7),
       (-12, -10),
       (-12, -11);



INSERT INTO planned_activity(id, template, type, with_trainer, created_by_id, created_for_id, date, interval_id, note, name, load, estimated_duration,
                             activity_id)
VALUES (-1, FALSE, '0', FALSE, -3, -2, PARSEDATETIME('26-08-2023', 'dd-MM-yyyy'), -1, 'test #1', 'test #1', '1', 60, null),
       (-2, TRUE, '1', FALSE, -3, null, null, -6, 'test #2', 'test #2', '1', 60, null),
       (-3, FALSE, '2', FALSE, -3, -1, PARSEDATETIME('26-12-2023', 'dd-MM-yyyy'), -2, 'test #3', 'test #3', '1', 60, null),
       (-4, FALSE, '3', FALSE, -3, -2, PARSEDATETIME('26-10-2023', 'dd-MM-yyyy'), -5, 'test #4', 'test #4', '1', 60, null),
       (-5, FALSE, '4', FALSE, -3, -2, PARSEDATETIME('26-10-2023', 'dd-MM-yyyy'), -3, 'test #5', 'test #5', '1', 60, null),
       (-6, FALSE, '5', FALSE, -3, -2, PARSEDATETIME('12-12-2023', 'dd-MM-yyyy'), -4, 'test #6', 'test #6', '1', 60, null),
       (-7, FALSE, '1', FALSE, -4, -4, PARSEDATETIME('30-09-2023', 'dd-MM-yyyy'), -12, 'test #7', '7x(1km P:1min)', '1', 60, null),
       (-8, TRUE, '1', TRUE, -3, null, null, -13, 'test #8 csp #1', 'csp #1', '2', 60, null),
       (-9, TRUE, '1', TRUE, -3, null, null, -14, 'test #9 csp #2', 'csp #2', '1', 60, null),
       (-10, FALSE, '1', TRUE, -3, -2, null, -15, 'test #10 csp #3', 'csp #3', '1', 60, null),
       (-11, FALSE, '1', TRUE, -3, -2, null, -16, 'test #11 csp #4', 'csp #4', '1', 60, null);

INSERT INTO time_constraints(id,user_id,is_blacklist,title)
VALUES (-1,-2,false,'white'),
       (-2,-2,true,'black'),
       (-3,-3,false,'white'),
       (-4,-3,true,'black');

INSERT INTO weekly_time_constraint(id,weekday,start_time,end_time)
VALUES (-1,0,'08:00:00','20:00:00'),
       (-2,0,'12:00:00','13:00:00'),
       (-3,0,'08:00:00','20:00:00'),
       (-4,0,'12:00:00','13:00:00');

INSERT INTO activity(id, user_id, accuracy, average_bpm, min_bpm, max_bpm, distance, spent_kcal, cadence, avg_power, max_power, start_time, end_time, activity_type)
VALUES (-1, -1, 0, 100, 70, 120, 10000, 800, 80, 100, 120, PARSEDATETIME('18-12-2023 12:10', 'dd-MM-yyyy HH:mm'), PARSEDATETIME('18-12-2023 14:15', 'dd-MM-yyyy HH:mm'), 1),
       (-2, -2, 0, 100, 70, 120, 10000, 800, 80, 100, 120, PARSEDATETIME('18-12-2023 12:10', 'dd-MM-yyyy HH:mm'), PARSEDATETIME('18-12-2023 14:15', 'dd-MM-yyyy HH:mm'), 1)
