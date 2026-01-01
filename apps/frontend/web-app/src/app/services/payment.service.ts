import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

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

  constructor(private http: HttpClient) { }

  createInternalTransfer(request: PaymentRequest, userId: number = 1): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/internal?userId=${userId}`,
      request
    );
  }

  createSepaTransfer(request: PaymentRequest, userId: number = 1): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/sepa?userId=${userId}`,
      request
    );
  }

  createInstantTransfer(request: PaymentRequest, userId: number = 1): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/instant?userId=${userId}`,
      request
    );
  }

  createMobileRecharge(request: PaymentRequest, userId: number = 1): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/mobile-recharge?userId=${userId}`,
      request
    );
  }

  getPayment(paymentId: number): Observable<PaymentResponse> {
    return this.http.get<PaymentResponse>(
      `${this.apiUrl}/${paymentId}`
    );
  }

  getUserPayments(userId: number = 1): Observable<PaymentResponse[]> {
    return this.http.get<PaymentResponse[]>(
      `${this.apiUrl}/user?userId=${userId}`
    );
  }

  authorizePayment(paymentId: number, otpCode: string): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/${paymentId}/authorize`,
      { otpCode }
    );
  }
}
