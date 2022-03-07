import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManualEventCodeEntryComponent } from './manual-event-code-entry.component';

describe('ManualEventCodeEntryComponent', () => {
  let component: ManualEventCodeEntryComponent;
  let fixture: ComponentFixture<ManualEventCodeEntryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ManualEventCodeEntryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManualEventCodeEntryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
