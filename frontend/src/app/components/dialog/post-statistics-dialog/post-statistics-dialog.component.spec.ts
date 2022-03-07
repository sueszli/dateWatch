import {ComponentFixture, TestBed} from '@angular/core/testing';

import {PostStatisticsDialogComponent} from './post-statistics-dialog.component';

describe('PostStatisticsDialogComponent', () => {
  let component: PostStatisticsDialogComponent;
  let fixture: ComponentFixture<PostStatisticsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PostStatisticsDialogComponent]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PostStatisticsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
