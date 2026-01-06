import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { KycWaitingComponent } from './kyc-waiting';

describe('KycWaitingComponent', () => {
  let component: KycWaitingComponent;
  let fixture: ComponentFixture<KycWaitingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KycWaitingComponent, RouterTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(KycWaitingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
