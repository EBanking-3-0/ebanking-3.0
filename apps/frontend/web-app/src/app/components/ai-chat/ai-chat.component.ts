import { Component, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiAssistantService } from '../../services/ai-assistant.service';

interface Message {
  role: 'user' | 'assistant';
  content: string;
  intent?: string;
  actionExecuted?: string;
  actionResult?: any;
  timestamp: Date;
}

@Component({
  selector: 'app-ai-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-chat.component.html',
  styleUrls: ['./ai-chat.component.scss']
})
export class AiChatComponent {
  message: string = '';
  messages: Message[] = [];
  conversationId: string = '';
  sessionId: string = '';
  loading: boolean = false;

  // Action execution
  actionName: string = '';
  actionParams: string = '{}';
  actionResult: any = null;

  constructor(
    private aiService: AiAssistantService,
    private ngZone: NgZone
  ) {}

  sendMessage() {
    if (!this.message.trim()) return;

    const userMessage: Message = {
      role: 'user',
      content: this.message,
      timestamp: new Date()
    };
    this.messages.push(userMessage);

    this.loading = true;
    const messageText = this.message;
    this.message = '';

    this.aiService.sendMessage(messageText, this.conversationId, this.sessionId)
      .subscribe({
        next: (response) => {
          this.ngZone.run(() => {
            console.log('AI Response:', response);
            
            const assistantMessage: Message = {
              role: 'assistant',
              content: response.response,
              intent: response.intent,
              actionExecuted: response.actionExecuted,
              actionResult: response.actionResult,
              timestamp: new Date()
            };
            this.messages.push(assistantMessage);
            
            // Update conversation IDs
            if (response.conversationId) {
              this.conversationId = response.conversationId;
            }
            if (response.sessionId) {
              this.sessionId = response.sessionId;
            }
            
            this.loading = false;
          });
        },
        error: (error) => {
          this.ngZone.run(() => {
            console.error('Error sending message:', error);
            const errorMessage: Message = {
              role: 'assistant',
              content: 'Sorry, I encountered an error. Please try again.',
              timestamp: new Date()
            };
            this.messages.push(errorMessage);
            this.loading = false;
          });
        },
        complete: () => {
          this.ngZone.run(() => {
            console.log('Message send completed');
            this.loading = false;
          });
        }
      });
  }

  executeAction() {
    if (!this.actionName.trim()) return;

    try {
      const params = JSON.parse(this.actionParams);
      this.loading = true;

      this.aiService.executeAction(this.actionName, params)
        .subscribe({
          next: (result) => {
            this.actionResult = result;
            this.loading = false;
          },
          error: (error) => {
            console.error('Error executing action:', error);
            this.actionResult = { error: error.message || 'Action execution failed' };
            this.loading = false;
          }
        });
    } catch (e) {
      this.actionResult = { error: 'Invalid JSON parameters' };
    }
  }

  onEnterKey(event: KeyboardEvent) {
    if (event.shiftKey) {
      // Allow shift+enter for new line
      return;
    }
    event.preventDefault();
    this.sendMessage();
  }

  clearChat() {
    this.messages = [];
    this.conversationId = '';
    this.sessionId = '';
  }

  clearActionResult() {
    this.actionResult = null;
  }
}
