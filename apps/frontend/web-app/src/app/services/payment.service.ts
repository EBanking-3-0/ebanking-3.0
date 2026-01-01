import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface PaymentRequest {
  fromAccountId: number;
  toAccountId?: number;
  toIban?: string;
  toAccountNumber?: string;
  phoneNumber?: string;
  countryCode?: string;
  amount: number;
  currency: string;
  description?: string;
  idempotencyKey: string;
}

export interface PaymentResponse {
  paymentId: number;
  transactionId: string;
  status: string;
  amount: number;
  currency: string;
  message: string;
  createdAt: string;
  estimatedCompletionDate?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = environment.paymentApiUrl;

  constructor(private http: HttpClient) { }

  createInternalTransfer(request: PaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/internal`,
      request
    );
  }

  createSepaTransfer(request: PaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/sepa`,
      request
    );
  }

  createInstantTransfer(request: PaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/instant`,
      request
    );
  }

  createMobileRecharge(request: PaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/mobile-recharge`,
      request
    );
  }

  getPayment(paymentId: number): Observable<PaymentResponse> {
    return this.http.get<PaymentResponse>(
      `${this.apiUrl}/${paymentId}`
    );
  }

  getUserPayments(): Observable<PaymentResponse[]> {
    return this.http.get<PaymentResponse[]>(
      `${this.apiUrl}/user`
    );
  }
}
