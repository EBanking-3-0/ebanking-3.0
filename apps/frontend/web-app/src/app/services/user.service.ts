import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  status: string;
}

const GET_USERS = gql`
  query GetUsers {
    users {
      id
      email
      firstName
      lastName
      phone
      status
    }
  }
`;

const GET_ME = gql`
  query GetMe {
    me {
      id
      email
      firstName
      lastName
      phone
      status
    }
  }
`;

const GET_USER = gql`
  query GetUser($id: ID!) {
    user(id: $id) {
      id
      email
      firstName
      lastName
      phone
      status
    }
  }
`;

const CREATE_USER = gql`
  mutation CreateUser($input: CreateUserInput!) {
    createUser(input: $input) {
      id
      email
      firstName
      lastName
      phone
      status
    }
  }
`;

const UPDATE_USER = gql`
  mutation UpdateUser($id: ID!, $input: UpdateUserInput!) {
    updateUser(id: $id, input: $input) {
      id
      email
      firstName
      lastName
      phone
      status
    }
  }
`;

const DELETE_USER = gql`
  mutation DeleteUser($id: ID!) {
    deleteUser(id: $id)
  }
`;

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private apollo: Apollo) {}

  getUsers(): Observable<User[]> {
    return this.apollo
      .watchQuery<{ users: User[] }>({
        query: GET_USERS,
      })
      .valueChanges.pipe(
        map(result => (result.data?.users || []) as User[])
      );
  }

  getUser(id: string): Observable<User> {
    return this.apollo
      .query<{ user: User }>({
        query: GET_USER,
        variables: { id },
        fetchPolicy: 'network-only'
      })
      .pipe(
        map(result => result.data?.user as User)
      );
  }

  createUser(user: Omit<User, 'id' | 'status'>): Observable<User> {
    return this.apollo
      .mutate<{ createUser: User }>({
        mutation: CREATE_USER,
        variables: { input: user },
        refetchQueries: [{ query: GET_USERS }],
      })
      .pipe(map(result => result.data?.createUser as User));
  }

  updateUser(id: string, user: Omit<User, 'id' | 'email' | 'status'>): Observable<User> {
    return this.apollo
      .mutate<{ updateUser: User }>({
        mutation: UPDATE_USER,
        variables: { id, input: user },
        refetchQueries: [{ query: GET_USERS }],
      })
      .pipe(map(result => result.data?.updateUser as User));
  }

  deleteUser(id: string): Observable<boolean> {
    return this.apollo
      .mutate<{ deleteUser: boolean }>({
        mutation: DELETE_USER,
        variables: { id },
        refetchQueries: [{ query: GET_USERS }],
      })
      .pipe(map(result => result.data?.deleteUser as boolean));
  }

  getMe(): Observable<User> {
    return this.apollo
      .query<{ me: User }>({
        query: GET_ME,
        fetchPolicy: 'network-only'
      })
      .pipe(
        map(result => result.data?.me as User)
      );
  }
}
