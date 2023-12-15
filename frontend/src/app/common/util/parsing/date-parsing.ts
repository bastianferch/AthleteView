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
