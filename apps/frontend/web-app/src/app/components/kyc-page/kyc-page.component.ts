import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { environment } from '../../../environments/environment';

interface ConsentItem {
  key: string; // e.g. "MARKETING_EMAIL"
  label: string; // Human readable label
}

@Component({
  selector: 'app-kyc-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './kyc-page.component.html',
  styleUrl: './kyc-page.component.scss',
})
export class KycPageComponent implements OnInit {
  kycForm: FormGroup;
  submissionError: string | null = null;
  submissionSuccess = false;
  submitting = false;

  // List of consents matching GdprConsent.ConsentType
  consentTypes: ConsentItem[] = [
    { key: 'MARKETING_EMAIL', label: 'Receive marketing emails' },
    { key: 'MARKETING_SMS', label: 'Receive marketing SMS' },
    { key: 'MARKETING_PHONE', label: 'Receive marketing phone calls' },
    { key: 'PERSONALIZED_OFFERS', label: 'Receive personalized offers' },
    { key: 'DATA_SHARING_PARTNERS', label: 'Share data with trusted partners' },
    { key: 'ANALYTICS_IMPROVEMENT', label: 'Help improve analytics and product' },
    { key: 'OPEN_BANKING_SHARING', label: 'Enable open banking data sharing' },
  ];

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
  ) {
    // Build the gdprConsents form group with false as default
    const gdprControls: any = {};
    this.consentTypes.forEach((c) => {
      gdprControls[c.key] = [false]; // unchecked by default
    });

    this.kycForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      phone: ['', Validators.required],
      addressLine1: [''],
      addressLine2: [''],
      city: [''],
      postalCode: [''],
      country: [''],
      cinNumber: ['', Validators.required],
      cinImage: [null, Validators.required],
      selfieImage: [null, Validators.required],

      // NEW: GDPR consents nested group
      gdprConsents: this.fb.group(gdprControls),
    });
  }

  ngOnInit(): void {
    // Pre-fill logic if you want...
  }

  onFileChange(event: Event, field: 'cinImage' | 'selfieImage'): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      const file = input.files[0];
      this.kycForm.patchValue({ [field]: file });
      this.kycForm.get(field)?.markAsTouched();
    }
  }

  onSubmit(): void {
    if (this.kycForm.invalid) {
      this.kycForm.markAllAsTouched();
      return;
    }

    this.submissionError = null;
    this.submissionSuccess = false;
    this.submitting = true;

    const formValue = this.kycForm.value;

    // Extract files
    const { cinImage, selfieImage, gdprConsents, ...inputData } = formValue;

    // Merge gdprConsents into the main payload (it will be sent as part of JSON)
    const payload = {
      ...inputData,
      gdprConsents: this.transformConsentsToMap(gdprConsents),
    };

    if (!(cinImage instanceof File) || !(selfieImage instanceof File)) {
      this.submissionError = 'Please select valid image files for CIN and selfie.';
      this.submitting = false;
      return;
    }

    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
    formData.append('cinImage', cinImage, cinImage.name);
    formData.append('selfieImage', selfieImage, selfieImage.name);

    this.http
      .post(`${environment.apiRestUrl}/api/v1/kyc`, formData, {
        withCredentials: true,
      })
      .pipe(
        catchError((err: any) => {
          console.error('KYC submission failed:', err);
          let message = 'An error occurred during submission.';
          // ... your existing error handling ...
          this.submissionError = message;
          this.submitting = false;
          return of(null);
        }),
      )
      .subscribe((response: any) => {
        this.submitting = false;
        if (response) {
          console.log('KYC submitted successfully:', response);
          this.submissionSuccess = true;
        }
      });
  }

  // Helper: convert { MARKETING_EMAIL: true, ... } → map with only granted consents
  // (your backend ignores false values anyway, but sending only true is cleaner)
  private transformConsentsToMap(consents: any): { [key: string]: boolean } {
    const map: { [key: string]: boolean } = {};
    Object.keys(consents).forEach((key) => {
      if (consents[key] === true) {
        map[key] = true;
      }
    });
    return map;
  }
}
