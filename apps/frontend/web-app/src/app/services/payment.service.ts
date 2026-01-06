import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

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
  type?: string;
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

const INITIATE_INTERNAL_TRANSFER = gql`
  mutation InitiateInternalTransfer($input: PaymentRequest!) {
    initiateInternalTransfer(input: $input) {
      paymentId
      transactionId
      status
      amount
      currency
      message
      createdAt
    }
  }
`;

const INITIATE_SEPA_TRANSFER = gql`
  mutation InitiateSepaTransfer($input: PaymentRequest!) {
    initiateSepaTransfer(input: $input) {
      paymentId
      transactionId
      status
      amount
      currency
      message
      createdAt
    }
  }
`;

const INITIATE_INSTANT_TRANSFER = gql`
  mutation InitiateInstantTransfer($input: PaymentRequest!) {
    initiateInstantTransfer(input: $input) {
      paymentId
      transactionId
      status
      amount
      currency
      message
      createdAt
    }
  }
`;

const INITIATE_MOBILE_RECHARGE = gql`
  mutation InitiateMobileRecharge($input: PaymentRequest!) {
    initiateMobileRecharge(input: $input) {
      paymentId
      transactionId
      status
      amount
      currency
      message
      createdAt
    }
  }
`;

const AUTHORIZE_PAYMENT = gql`
  mutation AuthorizePayment($paymentId: ID!, $otpCode: String!) {
    authorizePayment(paymentId: $paymentId, otpCode: $otpCode) {
      paymentId
      status
      message
    }
  }
`;

const MY_PAYMENTS = gql`
  query MyPayments {
    myPayments {
      paymentId
      transactionId
      status
      amount
      currency
      createdAt
      paymentType
    }
  }
`;

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  constructor(private apollo: Apollo) { }

  createInternalTransfer(request: PaymentRequest): Observable<PaymentResponse> {
    return this.apollo.mutate<{ initiateInternalTransfer: PaymentResponse }>({
      mutation: INITIATE_INTERNAL_TRANSFER,
      variables: { input: request }
    }).pipe(map(result => result.data!.initiateInternalTransfer));
  }

  createSepaTransfer(request: PaymentRequest): Observable<PaymentResponse> {
    return this.apollo.mutate<{ initiateSepaTransfer: PaymentResponse }>({
      mutation: INITIATE_SEPA_TRANSFER,
      variables: { input: request }
    }).pipe(map(result => result.data!.initiateSepaTransfer));
  }

  createInstantTransfer(request: PaymentRequest): Observable<PaymentResponse> {
    return this.apollo.mutate<{ initiateInstantTransfer: PaymentResponse }>({
      mutation: INITIATE_INSTANT_TRANSFER,
      variables: { input: request }
    }).pipe(map(result => result.data!.initiateInstantTransfer));
  }

  createMobileRecharge(request: PaymentRequest): Observable<PaymentResponse> {
    return this.apollo.mutate<{ initiateMobileRecharge: PaymentResponse }>({
      mutation: INITIATE_MOBILE_RECHARGE,
      variables: { input: request }
    }).pipe(map(result => result.data!.initiateMobileRecharge));
  }

  getUserPayments(): Observable<PaymentResponse[]> {
    return this.apollo.query<{ myPayments: PaymentResponse[] }>({
      query: MY_PAYMENTS,
      fetchPolicy: 'network-only'
    }).pipe(map(result => result.data?.myPayments || []));
  }

  authorizePayment(paymentId: number, otpCode: string): Observable<PaymentResponse> {
    return this.apollo.mutate<{ authorizePayment: PaymentResponse }>({
      mutation: AUTHORIZE_PAYMENT,
      variables: { paymentId, otpCode }
    }).pipe(map(result => result.data!.authorizePayment));
  }
}
