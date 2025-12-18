import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AppComponent } from './app.component';
import { KeycloakService } from 'keycloak-angular';
import { vi } from 'vitest';

describe('App', () => {
  beforeEach(async () => {
    const keycloakMock = {
      isLoggedIn: vi.fn().mockResolvedValue(false),
      getKeycloakInstance: vi.fn().mockReturnValue({ idTokenParsed: {} }),
      login: vi.fn(),
      logout: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: KeycloakService, useValue: keycloakMock },
      ],
    }).compileComponents();
  });

  it('should render title', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('E-Banking 3.0');
  });
});
