import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Apollo } from 'apollo-angular';
import { gql } from 'graphql-tag';
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
  styleUrls: ['./kyc-page.component.scss'],
})
export class KycPageComponent implements OnInit {
  userProfile: any;
  kycForm: FormGroup;
  loading = true;
  error: any;
  submissionError: any;

  constructor(private apollo: Apollo, private fb: FormBuilder) {
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
    this.apollo
      .watchQuery<any>({
        query: GET_USER_PROFILE,
      })
      .valueChanges.subscribe(({ data, loading, error }) => {
        this.loading = loading;
        this.error = error;
        if (data && data.me) {
          this.userProfile = data.me;
          this.kycForm.patchValue(this.userProfile);
        }
      });
  }

  onFileChange(event: any, field: string) {
    if (event.target.files.length > 0) {
      const file = event.target.files[0];
      this.kycForm.patchValue({
        [field]: file,
      });
    }
  }

  onSubmit() {
    if (this.kycForm.invalid) {
      return;
    }

    this.submissionError = null;
    const { cinImage, selfieImage, ...formData } = this.kycForm.value;

    this.apollo
      .mutate({
        mutation: SUBMIT_KYC,
        variables: {
          input: formData,
          cinImage: cinImage,
          selfieImage: selfieImage,
        },
        context: {
          useMultipart: true,
        },
      })
      .pipe(
        catchError((error) => {
          this.submissionError = error;
          return of(null);
        })
      )
      .subscribe((result) => {
        if (result && !result.error) {
          // Handle successful submission
          console.log('KYC Submitted:', result);
          // Refresh user profile
          this.apollo.client.refetchQueries({
            include: [GET_USER_PROFILE],
          });
        }
      });
  }
}
