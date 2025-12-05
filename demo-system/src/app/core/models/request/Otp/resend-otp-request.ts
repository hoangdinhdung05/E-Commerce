export interface ResendOtpRequest {
  email: string;
  type: 'VERIFY_EMAIL' | 'FORGOT_PASSWORD';
}
