import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EntranceCodeConfirmedComponent } from './entrance-code-confirmed.component';

describe('EntranceCodeConfirmedComponent', () => {
  let component: EntranceCodeConfirmedComponent;
  let fixture: ComponentFixture<EntranceCodeConfirmedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EntranceCodeConfirmedComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EntranceCodeConfirmedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
