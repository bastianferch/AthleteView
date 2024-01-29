export interface PreferencesDto {
  emailNotifications: boolean,
  commentNotifications: PreferenceNotificationType,
  ratingNotifications: PreferenceNotificationType,
  otherNotifications: PreferenceNotificationType,
  shareHealthWithTrainer: boolean,
}


export enum PreferenceNotificationType {
  EMAIL = "EMAIL",
  PUSH = "PUSH",
  NONE = "NONE",
  BOTH = "BOTH",
}

export const PreferenceNotificationMapper = new Map<PreferenceNotificationType, string>([
  [PreferenceNotificationType.EMAIL, 'Email'],
  [PreferenceNotificationType.PUSH, 'Push'],
  [PreferenceNotificationType.NONE, 'None'],
  [PreferenceNotificationType.BOTH, 'Email & Push'],
]);
