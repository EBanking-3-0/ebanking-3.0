import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Apollo, gql } from 'apollo-angular';

export interface ChatResponse {
  response: string;
  intent?: string;
  actionExecuted?: string;
  actionResult?: any;
  conversationId?: string;
  sessionId?: string;
}

const SEND_CHAT_MESSAGE = gql`
  mutation SendChatMessage($input: ChatMessageInput!) {
    sendChatMessage(input: $input) {
      conversationId
      sessionId
      response
      intent
      actionExecuted
      actionResult
    }
  }
`;

const EXECUTE_ACTION = gql`
  mutation ExecuteAction($input: ActionExecutionInput!) {
    executeAction(input: $input) {
      success
      result
      error
    }
  }
`;

@Injectable({
  providedIn: 'root',
})
export class AiAssistantService {
  constructor(private apollo: Apollo) {}

  sendMessage(
    message: string,
    conversationId?: string,
    sessionId?: string,
  ): Observable<ChatResponse> {
    console.log('Sending message:', { message, conversationId, sessionId });

    return this.apollo
      .mutate<{ sendChatMessage: ChatResponse }>({
        mutation: SEND_CHAT_MESSAGE,
        variables: {
          input: {
            message,
            conversationId,
            sessionId,
          },
        },
      })
      .pipe(
        map((result) => {
          console.log('GraphQL Result:', result);
          if (!result.data) {
            throw new Error('No data received from server');
          }
          return result.data.sendChatMessage;
        }),
      );
  }

  executeAction(actionName: string, parameters: Record<string, any>): Observable<any> {
    return this.apollo
      .mutate<{ executeAction: any }>({
        mutation: EXECUTE_ACTION,
        variables: {
          input: {
            actionName,
            parameters: JSON.stringify(parameters),
          },
        },
      })
      .pipe(map((result) => result.data!.executeAction));
  }
}
