import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EventPipelineComponent } from './event-pipeline.component';

describe('EventPipelineComponent', () => {
  let component: EventPipelineComponent;
  let fixture: ComponentFixture<EventPipelineComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EventPipelineComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EventPipelineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
