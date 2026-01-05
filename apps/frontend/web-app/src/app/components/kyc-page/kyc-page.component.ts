import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Apollo, gql } from 'apollo-angular';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { CommonModule } from '@angular/common';

const GET_USER_PROFILE = gql`
  query Me {
    me {
      id
      firstName
      lastName
      email
      phone
      addressLine1
      addressLine2
      city
      postalCode
      country
      status
      kycStatus
    }
  }
`;

const SUBMIT_KYC = gql`
  mutation SubmitKyc($input: KycRequestInput!, $cinImage: Upload!, $selfieImage: Upload!) {
    submitKyc(input: $input, cinImage: $cinImage, selfieImage: $selfieImage) {
      status
      verifiedAt
    }
  }
`;

@Component({
  selector: 'app-kyc-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './kyc-page.component.html',
  styleUrl: './kyc-page.component.scss',
})
export class KycPageComponent implements OnInit {
  userProfile: any = null;
  kycForm: FormGroup;
  loading = true;
  error: any = null;
  submissionError: string | null = null;

  constructor(
    private apollo: Apollo,
    private fb: FormBuilder
  ) {
    console.log('KycPageComponent constructor');
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
    });
  }

  ngOnInit(): void {
    console.log('KycPageComponent ngOnInit');
    this.apollo
      .watchQuery<{ me: any }>({
        query: GET_USER_PROFILE,
      })
      .valueChanges
      .subscribe(({ data, loading, error }) => {
        console.log('watchQuery subscription', { data, loading, error });
        this.loading = loading;
        this.error = error;

        if (data?.me) {
          this.userProfile = data.me;
          this.kycForm.patchValue(this.userProfile);
        }
      });
  }

  onFileChange(event: Event, field: 'cinImage' | 'selfieImage'): void {
    console.log('onFileChange', { event, field });
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      const file = input.files[0];
      this.kycForm.patchValue({ [field]: file });
      this.kycForm.get(field)?.markAsTouched();
    }
  }

  onSubmit(): void {
    console.log('onSubmit');
    if (this.kycForm.invalid) {
      this.kycForm.markAllAsTouched();
      return;
    }

    this.submissionError = null;

    const formValue = this.kycForm.value;
    const { cinImage, selfieImage, ...inputData } = formValue;

    // Debug: check if we really have File objects
    console.log('=== BEFORE MUTATION DEBUG ===');
    console.log('cinImage:', cinImage instanceof File ? `File (${cinImage.name}, ${cinImage.size} bytes)` : typeof cinImage, cinImage);
    console.log('selfieImage:', selfieImage instanceof File ? `File (${selfieImage.name}, ${selfieImage.size} bytes)` : typeof selfieImage, selfieImage);
    console.log('===========================');

    // Safety: if not File, abort early
    if (!(cinImage instanceof File) || !(selfieImage instanceof File)) {
      this.submissionError = 'One or both files are not valid. Please select them again.';
      return;
    }

    this.apollo
      .mutate<{ submitKyc: { status: string; verifiedAt: string } }>({
        mutation: SUBMIT_KYC,
        variables: {
          input: inputData,
          cinImage,      // ← must be raw File
          selfieImage,   // ← must be raw File
        },
      })
      .pipe(
        catchError((err: unknown) => {
          console.error('KYC submission error:', err);
          let errorMessage = 'An unknown error occurred during submission.';

          if (err && typeof err === 'object') {
            const errorObj = err as any;
            if (Array.isArray(errorObj.graphQLErrors) && errorObj.graphQLErrors.length > 0) {
              errorMessage = errorObj.graphQLErrors[0].message;
            } else if (errorObj.networkError) {
              errorMessage = `Network error: ${errorObj.networkError.message || 'Connection failed'}`;
            }
          }

          this.submissionError = errorMessage;
          return of(null);
        })
      )
      .subscribe((result) => {
        console.log('mutate subscription', { result });
        if (result?.data?.submitKyc) {
          console.log('KYC submitted successfully:', result.data.submitKyc);
          this.apollo.client.refetchQueries({ include: [GET_USER_PROFILE] });
        }
      });
  }
}
