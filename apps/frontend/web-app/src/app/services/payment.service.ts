import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export interface PaymentRequest {
  fromAccountId: number;
  toAccountId?: number;
  toAccountNumber?: string;
  toIban?: string;
  beneficiaryName?: string;
  beneficiarySwiftBic?: string;
  phoneNumber?: string;
  countryCode?: string;
  merchantId?: string;
  invoiceReference?: string;
  amount: number;
  currency: string;
  type?: string; // INTERNAL_TRANSFER, SEPA_TRANSFER, SCT_INSTANT, MOBILE_RECHARGE, etc.
  description?: string;
  endToEndId?: string;
  idempotencyKey: string;
  ipAddress?: string;
  userAgent?: string;
}

export interface PaymentResponse {
  paymentId: number;
  transactionId: string;
  status: string;
  paymentType?: string;
  amount: number;
  currency: string;
  fees?: number;
  reference?: string;
  uetr?: string;
  message: string;
  createdAt: string;
  estimatedCompletionDate?: string;
}

export interface ScaVerificationRequest {
  otpCode: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = environment.paymentApiUrl;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  private getUserId(userId?: string): string {
    // If userId is provided, use it. Otherwise, try to get from AuthService.
    // If AuthService doesn't have it (not logged in?), fallback to empty string or handle error.
    // Ideally, we should throw if no user ID is available.
    const id = userId || this.authService.getCurrentUserId();
    if (!id) {
      console.warn('No user ID available for payment request');
      // Fallback or throw? For now, let's return a placeholder or throw.
      // Given the previous code used '1' as default, maybe we should be careful.
      // But the goal is to use the real ID.
      throw new Error('User not authenticated');
    }
    return id;
  }

  createInternalTransfer(request: PaymentRequest, userId?: string): Observable<PaymentResponse> {
    const id = this.getUserId(userId);
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/internal?userId=${id}`,
      request
    );
  }

  createSepaTransfer(request: PaymentRequest, userId?: string): Observable<PaymentResponse> {
    const id = this.getUserId(userId);
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/sepa?userId=${id}`,
      request
    );
  }

  createInstantTransfer(request: PaymentRequest, userId?: string): Observable<PaymentResponse> {
    const id = this.getUserId(userId);
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/instant?userId=${id}`,
      request
    );
  }

  createMobileRecharge(request: PaymentRequest, userId?: string): Observable<PaymentResponse> {
    const id = this.getUserId(userId);
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/mobile-recharge?userId=${id}`,
      request
    );
  }

  getPayment(paymentId: number): Observable<PaymentResponse> {
    return this.http.get<PaymentResponse>(
      `${this.apiUrl}/${paymentId}`
    );
  }

  getUserPayments(userId?: string): Observable<PaymentResponse[]> {
    const id = this.getUserId(userId);
    return this.http.get<PaymentResponse[]>(
      `${this.apiUrl}/user?userId=${id}`
    );
  }

  authorizePayment(paymentId: number, otpCode: string): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/${paymentId}/authorize`,
      { otpCode }
    );
  }
}
