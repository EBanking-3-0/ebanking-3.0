import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

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

@Injectable({
    providedIn: 'root'
})
export class AccountService {
    private apiUrl = environment.accountApiUrl;

    constructor(private http: HttpClient) { }

    getMyAccounts(userId: string): Observable<AccountDTO[]> {
        return this.http.get<AccountDTO[]>(`${this.apiUrl}/my-accounts`);
    }
}
