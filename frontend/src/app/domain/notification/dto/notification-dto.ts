export interface NotificationDto {
  id: number,
  header: string,
  body?: string,
  link?: string,
  read: boolean,
  timestamp: number,
}
