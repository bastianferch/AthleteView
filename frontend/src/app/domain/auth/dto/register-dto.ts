export interface RegisterDto {
  email: string,
  password: string,
  name: string,
  country?: string,
  zip?: string,
  height?: number,
  weight?: number,
  dob?: Date | string,
}
