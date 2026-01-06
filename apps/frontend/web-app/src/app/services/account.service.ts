import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface AccountDTO {
    id: number;
    accountNumber: string;
    userId: string;
    balance: number;
    currency: string;
    type: string;
    status: string;
    createdAt: string;
}

const MY_ACCOUNTS = gql`
  query MyAccounts {
    myAccounts {
      id
      accountNumber
      userId
      balance
      currency
      type
      status
      createdAt
    }
  }
`;

@Injectable({
    providedIn: 'root'
})
export class AccountService {

    constructor(private apollo: Apollo) { }

    getMyAccounts(): Observable<AccountDTO[]> {
        return this.apollo.query<{ myAccounts: AccountDTO[] }>({
            query: MY_ACCOUNTS,
            fetchPolicy: 'network-only'
        }).pipe(map(result => result.data?.myAccounts || []));
    }
}
