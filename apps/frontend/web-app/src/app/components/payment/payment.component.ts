import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PaymentService, PaymentRequest, PaymentResponse } from '../../services/payment.service';
import { AccountService, AccountDTO } from '../../services/account.service';
import Keycloak from 'keycloak-js';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './payment.component.html',
  styleUrl: './payment.component.scss'
})
export class PaymentComponent implements OnInit {
  activeTab: 'internal' | 'sepa' | 'instant' | 'mobile' = 'internal';

  internalForm!: FormGroup;
  sepaForm!: FormGroup;
  instantForm!: FormGroup;
  mobileForm!: FormGroup;

  loading = false;
  result: PaymentResponse | null = null;
  error: string | null = null;

  accounts: AccountDTO[] = [];
  accountsLoading = false;
  userPayments: PaymentResponse[] = [];
  paymentsLoading = false;
  showPaymentsHistory = false;

  // SCA (Strong Customer Authentication)
  scaRequired = false;
  scaPaymentId: number | null = null;
  otpForm!: FormGroup;
  otpError: string | null = null;

  currentUserId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private paymentService: PaymentService,
    private accountService: AccountService,
    private keycloak: Keycloak
  ) {
    this.initializeForms();
    this.initializeOtpForm();
  }

  async ngOnInit() {
    await this.loadCurrentUser();
    this.generateIdempotencyKey();
    this.loadAccounts();
    this.loadUserPayments();
  }

  async loadCurrentUser() {
    // TEMPORAIRE : Utiliser userId = 1 pour les tests (sans authentification)
    this.currentUserId = 1;
    
    // Code original commenté pour les tests
    /*
    try {
      if (this.keycloak.authenticated) {
        const tokenParsed = this.keycloak.idTokenParsed;
        // Try to get userId from token claims
        const userIdClaim = tokenParsed?.['userId'] || tokenParsed?.['sub'];
        if (userIdClaim) {
          this.currentUserId = typeof userIdClaim === 'string' 
            ? parseInt(userIdClaim, 10) 
            : userIdClaim;
        } else {
          // Fallback: try to parse from username or use default
          this.currentUserId = 1; // Default for demo
        }
      } else {
        this.currentUserId = 1; // Default for demo
      }
    } catch (error) {
      console.error('Error loading current user:', error);
      this.currentUserId = 1; // Default for demo
    }
    */
  }

  loadAccounts() {
    if (!this.currentUserId) {
      this.currentUserId = 1; // Fallback
    }
    this.accountsLoading = true;
    this.accountService.getMyAccounts(this.currentUserId).subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        this.accountsLoading = false;

        // Update forms with default account if available
        if (accounts.length > 0) {
          const defaultAccountId = accounts[0].id;
          this.internalForm.patchValue({ fromAccountId: defaultAccountId });
          this.sepaForm.patchValue({ fromAccountId: defaultAccountId });
          this.instantForm.patchValue({ fromAccountId: defaultAccountId });
          this.mobileForm.patchValue({ fromAccountId: defaultAccountId });
        }
      },
      error: (err) => {
        console.error('Failed to load accounts', err);
        this.error = 'Failed to load user accounts';
        this.accountsLoading = false;
      }
    });
  }

  loadUserPayments() {
    this.paymentsLoading = true;
    const userId = this.currentUserId || 1; // Utiliser currentUserId ou défaut à 1
    this.paymentService.getUserPayments(userId).subscribe({
      next: (payments) => {
        this.userPayments = payments;
        this.paymentsLoading = false;
      },
      error: (err) => {
        console.error('Failed to load payments', err);
        this.paymentsLoading = false;
      }
    });
  }

  initializeOtpForm() {
    this.otpForm = this.fb.group({
      otpCode: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
    });
  }

  initializeForms() {
    // Internal Transfer Form
    this.internalForm = this.fb.group({
      fromAccountId: [null, [Validators.required]],
      toAccountNumber: ['', [Validators.required]],
      amount: [100.00, [Validators.required, Validators.min(0.01)]],
      currency: ['EUR', [Validators.required, Validators.pattern(/^[A-Z]{3}$/)]],
      description: ['Test internal transfer']
    });

    // SEPA Transfer Form
    this.sepaForm = this.fb.group({
      fromAccountId: [null, [Validators.required]],
      toIban: ['FR1420041010050500013M02606', [Validators.required, Validators.pattern(/^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$/)]],
      beneficiaryName: ['', [Validators.required]],
      amount: [500.00, [Validators.required, Validators.min(0.01)]],
      currency: ['EUR', [Validators.required]],
      description: ['Test SEPA transfer']
    });

    // Instant Transfer Form
    this.instantForm = this.fb.group({
      fromAccountId: [null, [Validators.required]],
      toIban: ['DE89370400440532013000', [Validators.required, Validators.pattern(/^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$/)]],
      beneficiaryName: ['', [Validators.required]],
      amount: [1000.00, [Validators.required, Validators.min(0.01), Validators.max(15000)]],
      currency: ['EUR', [Validators.required]],
      description: ['']
    });

    // Mobile Recharge Form
    this.mobileForm = this.fb.group({
      fromAccountId: [null, [Validators.required]],
      phoneNumber: ['+33612345678', [Validators.required, Validators.pattern(/^\+?[1-9]\d{1,14}$/)]],
      countryCode: ['FR', [Validators.required]],
      amount: [20.00, [Validators.required, Validators.min(0.01)]],
      currency: ['EUR', [Validators.required]]
    });
  }

  generateIdempotencyKey(): string {
    return `test-${Date.now()}-${Math.random().toString(36).substring(2, 11)}`;
  }

  setActiveTab(tab: 'internal' | 'sepa' | 'instant' | 'mobile') {
    this.activeTab = tab;
    this.result = null;
    this.error = null;
    this.generateIdempotencyKey();
  }

  getCurrentForm(): FormGroup {
    switch (this.activeTab) {
      case 'internal': return this.internalForm;
      case 'sepa': return this.sepaForm;
      case 'instant': return this.instantForm;
      case 'mobile': return this.mobileForm;
    }
  }

  onSubmit() {
    const form = this.getCurrentForm();
    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.error = null;
    this.result = null;

    const formValue = form.value;
    
    // Build PaymentRequest according to backend structure
    const request: PaymentRequest = {
      fromAccountId: formValue.fromAccountId,
      amount: formValue.amount,
      currency: formValue.currency,
      description: formValue.description || '',
      idempotencyKey: this.generateIdempotencyKey(),
      ipAddress: '', // Will be set by backend if needed
      userAgent: navigator.userAgent
    };

    // Add type-specific fields
    switch (this.activeTab) {
      case 'internal':
        request.toAccountNumber = formValue.toAccountNumber;
        request.type = 'INTERNAL_TRANSFER';
        break;
      case 'sepa':
        request.toIban = formValue.toIban;
        request.beneficiaryName = formValue.beneficiaryName;
        request.type = 'SEPA_TRANSFER';
        break;
      case 'instant':
        request.toIban = formValue.toIban;
        request.beneficiaryName = formValue.beneficiaryName;
        request.type = 'SCT_INSTANT'; // Backend expects SCT_INSTANT (not INSTANT_TRANSFER)
        break;
      case 'mobile':
        request.phoneNumber = formValue.phoneNumber;
        request.countryCode = formValue.countryCode;
        request.type = 'MOBILE_RECHARGE';
        break;
    }

    let paymentObservable: Observable<PaymentResponse>;
    const userId = this.currentUserId || 1; // Utiliser currentUserId ou défaut à 1

    switch (this.activeTab) {
      case 'internal':
        paymentObservable = this.paymentService.createInternalTransfer(request, userId);
        break;
      case 'sepa':
        paymentObservable = this.paymentService.createSepaTransfer(request, userId);
        break;
      case 'instant':
        paymentObservable = this.paymentService.createInstantTransfer(request, userId);
        break;
      case 'mobile':
        paymentObservable = this.paymentService.createMobileRecharge(request, userId);
        break;
    }

    paymentObservable.subscribe({
      next: (response) => {
        this.result = response;
        this.loading = false;
        // Check if SCA is required
        if (response.status === 'AUTHORIZED' && response.message?.includes('SCA') || 
            response.message === 'SCA_REQUIRED' || 
            response.status === 'VALIDATED') {
          // Handle SCA flow
          this.scaRequired = true;
          this.scaPaymentId = response.paymentId;
          this.otpForm.reset();
        } else {
          this.generateIdempotencyKey();
          this.loadUserPayments(); // Refresh payments list
        }
      },
      error: (err) => {
        this.error = err.error?.message || err.message || 'An error occurred';
        this.loading = false;
        console.error('Payment error:', err);
      }
    });
  }

  onAuthorizePayment() {
    if (this.otpForm.invalid || !this.scaPaymentId) {
      this.otpForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.otpError = null;

    this.paymentService.authorizePayment(
      this.scaPaymentId,
      this.otpForm.value.otpCode
    ).subscribe({
      next: (response) => {
        this.result = response;
        this.loading = false;
        this.scaRequired = false;
        this.scaPaymentId = null;
        this.otpForm.reset();
        this.generateIdempotencyKey();
        this.loadUserPayments(); // Refresh payments list
      },
      error: (err) => {
        this.otpError = err.error?.message || err.message || 'Invalid OTP code';
        this.loading = false;
        console.error('Authorization error:', err);
      }
    });
  }

  cancelSca() {
    this.scaRequired = false;
    this.scaPaymentId = null;
    this.otpForm.reset();
    this.otpError = null;
    this.result = null;
  }

  togglePaymentsHistory() {
    this.showPaymentsHistory = !this.showPaymentsHistory;
    if (this.showPaymentsHistory) {
      this.loadUserPayments();
    }
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'COMPLETED':
      case 'SETTLED': return 'status-completed';
      case 'PROCESSING':
      case 'AUTHORIZED':
      case 'RESERVED':
      case 'SENT': return 'status-processing';
      case 'FAILED':
      case 'REJECTED': return 'status-failed';
      case 'COMPENSATED': return 'status-compensated';
      case 'CREATED':
      case 'VALIDATED': return 'status-pending';
      case 'CANCELLED': return 'status-cancelled';
      default: return 'status-default';
    }
  }

  getStatusLabel(status: string): string {
    switch (status?.toUpperCase()) {
      case 'COMPLETED': return 'Completed';
      case 'SETTLED': return 'Settled';
      case 'PROCESSING': return 'Processing';
      case 'AUTHORIZED': return 'Authorized';
      case 'RESERVED': return 'Reserved';
      case 'SENT': return 'Sent';
      case 'FAILED': return 'Failed';
      case 'REJECTED': return 'Rejected';
      case 'COMPENSATED': return 'Compensated';
      case 'CREATED': return 'Created';
      case 'VALIDATED': return 'Validated';
      case 'CANCELLED': return 'Cancelled';
      default: return status || 'Unknown';
    }
  }

  getPaymentTypeLabel(paymentType: string | undefined): string {
    if (!paymentType) return 'N/A';
    switch (paymentType.toUpperCase()) {
      case 'INTERNAL_TRANSFER': return 'Internal';
      case 'SEPA_TRANSFER': return 'SEPA';
      case 'SCT_INSTANT': return 'Instant';
      case 'MOBILE_RECHARGE': return 'Mobile';
      case 'SWIFT_TRANSFER': return 'SWIFT';
      case 'MERCHANT_PAYMENT': return 'Merchant';
      default: return paymentType;
    }
  }
}
