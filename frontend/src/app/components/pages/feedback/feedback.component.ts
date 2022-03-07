import {Component, OnInit} from '@angular/core';
import {Feedback} from '../../../dtos/feedback';
import {EventService} from '../../../services/event.service';
import {NotificationService} from '../../../services/notification.service';

@Component({
  selector: 'app-feedback',
  templateUrl: './feedback.component.html',
  styleUrls: ['./feedback.component.scss']
})
export class FeedbackComponent implements OnInit {

  sortedFeedbackMap: Map<string, string[]> = new Map<string, string[]>();
  sortedFeedback: { name: string; messages: string[] }[] = [];

  constructor(
    private eventService: EventService,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.loadFeedback();
  }

  loadFeedback() {
    this.eventService.getAllFeedback().subscribe({
      next: feedback => {
        console.log('received feedback', feedback);
        feedback.forEach(f => this.sortByEvent(f));
        this.sortedFeedback = Array.from(this.sortedFeedbackMap, ([name, messages]) => ({name, messages}));
        console.log(this.sortedFeedback);
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }

  private sortByEvent(feedback: Feedback): void {
    if (this.sortedFeedbackMap.has(feedback.eventTitle)) {
      this.sortedFeedbackMap.get(feedback.eventTitle).push(feedback.message);
    } else {
      this.sortedFeedbackMap.set(feedback.eventTitle, [feedback.message]);
    }
  }
}
