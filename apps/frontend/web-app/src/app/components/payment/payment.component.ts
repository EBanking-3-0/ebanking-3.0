import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PaymentService, PaymentRequest, PaymentResponse } from '../../services/payment.service';
import { AccountService, AccountDTO } from '../../services/account.service';
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

  constructor(
    private fb: FormBuilder,
    private paymentService: PaymentService,
    private accountService: AccountService
  ) {
    this.initializeForms();
  }

  ngOnInit() {
    this.generateIdempotencyKey();
    this.loadAccounts();
  }

  loadAccounts() {
    this.accountsLoading = true;
    // Hardcoded user 1 for demo purposes
    this.accountService.getMyAccounts(1).subscribe({
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
      amount: [500.00, [Validators.required, Validators.min(0.01)]],
      currency: ['EUR', [Validators.required]],
      description: ['Test SEPA transfer']
    });

    // Instant Transfer Form
    this.instantForm = this.fb.group({
      fromAccountId: [null, [Validators.required]],
      toIban: ['DE89370400440532013000', [Validators.required, Validators.pattern(/^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$/)]],
      amount: [1000.00, [Validators.required, Validators.min(0.01), Validators.max(15000)]],
      currency: ['EUR', [Validators.required]]
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
    const request: PaymentRequest = {
      ...formValue,
      idempotencyKey: this.generateIdempotencyKey()
    };

    let paymentObservable: Observable<PaymentResponse>;

    switch (this.activeTab) {
      case 'internal':
        paymentObservable = this.paymentService.createInternalTransfer(request);
        break;
      case 'sepa':
        paymentObservable = this.paymentService.createSepaTransfer(request);
        break;
      case 'instant':
        paymentObservable = this.paymentService.createInstantTransfer(request);
        break;
      case 'mobile':
        paymentObservable = this.paymentService.createMobileRecharge(request);
        break;
    }

    paymentObservable.subscribe({
      next: (response) => {
        this.result = response;
        this.loading = false;
        // Don't full reset, keeps defaults. Re-gen key
        this.generateIdempotencyKey();
        // Maybe refresh accounts or balance here?
      },
      error: (err) => {
        this.error = err.error?.message || err.message || 'An error occurred';
        this.loading = false;
        console.error('Payment error:', err);
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'COMPLETED': return 'status-completed';
      case 'PROCESSING': return 'status-processing';
      case 'FAILED': return 'status-failed';
      case 'COMPENSATED': return 'status-compensated';
      default: return 'status-default';
    }
  }
}
