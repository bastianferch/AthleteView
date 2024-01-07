import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root',
})
export class DateParsing {
  /**
   * Parses date from "Tue Jul 11 2023 19:15:20 GMT+0200 (Mitteleuropäische Sommerzeit)" to "2023-07-11". <br>
   * Returns null in case of invalid param type.
   *
   * @param date is like "Tue Jul 11 2023 19:15:20 GMT+0200 (Mitteleuropäische Sommerzeit)"
   * @return date like "xxxx-xx-xx"
   */
  toHyphenDate(date: Date): string {
    if (!date) {
      return null;
    }
    return this.hyphenDate(date.getFullYear(), date.getMonth() + 1, date.getDate());
  }

  parseNumbersIntoDate(numbers: number[]): Date {
    const str: string[] = []
    numbers.forEach((num) => {
      if (num.toString().length === 1) {
        str.push("0" + num.toString())
      } else {
        str.push(num.toString())
      }
    });
    return new Date(str[0] + "-" + str[1] + "-" + str[2] + "T" + str[3] + ":" + str[4])
  }


  // returns a string representation of the notifications timestamp (as a localized date string)
  // instead of today's or yesterday's date, it returns "today" or "yesterday".
  // exact time is only returned if timestamp is from today or yesterday.
  getDateAwareString(d: number | Date) {
    const date = new Date(d);
    const now = new Date();

    let dateString = "";
    // format: "hh:mm"
    let timeString = new Intl.DateTimeFormat(
      undefined,
      { hour: '2-digit', minute: '2-digit', hour12: false },
    ).format(date);

    if (now.getDate() === date.getDate()
      && now.getMonth() === date.getMonth()
      && now.getFullYear() === date.getFullYear()) {
      dateString = "today";
    } else if (now.getDate() === date.getDate() + 1
      && now.getMonth() === date.getMonth()
      && now.getFullYear() === date.getFullYear()) {
      dateString = "yesterday";
    } else {
      dateString = new Intl.DateTimeFormat().format(date);
      timeString = "";
    }

    return (`${dateString}${timeString === '' ? '' : ', ' + timeString}`)
  }


  /**
   * Provides a date with a type "2022-06-31"
   *
   * @param year
   * @param month
   * @param day
   */
  private hyphenDate(year: string | number, month: string | number, day: string | number) {
    return `${year}-${Number(month) < 10 ? '0' + month : month}-${Number(day) < 10 ? '0' + day : day}`;
  }
}

export const dateFormatString = "yyyy-MM-dd'T'HH:mm:ssxxx" // ISO format
