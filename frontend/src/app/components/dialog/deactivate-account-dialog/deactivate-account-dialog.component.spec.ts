import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeactivateAccountDialogComponent } from './deactivate-account-dialog.component';

describe('DeactivateAccountDialogComponent', () => {
  let component: DeactivateAccountDialogComponent;
  let fixture: ComponentFixture<DeactivateAccountDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DeactivateAccountDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DeactivateAccountDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
