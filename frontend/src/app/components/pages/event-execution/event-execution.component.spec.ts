import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EventExecutionComponent } from './event-execution.component';

describe('EventExecutionComponent', () => {
  let component: EventExecutionComponent;
  let fixture: ComponentFixture<EventExecutionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EventExecutionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EventExecutionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
