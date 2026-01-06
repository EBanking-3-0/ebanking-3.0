import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KycWaiting } from './kyc-waiting';

describe('KycWaiting', () => {
  let component: KycWaiting;
  let fixture: ComponentFixture<KycWaiting>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KycWaiting]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KycWaiting);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
