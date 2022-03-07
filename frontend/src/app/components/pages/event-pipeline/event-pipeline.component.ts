import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-event-pipeline',
  templateUrl: './event-pipeline.component.html',
  styleUrls: ['./event-pipeline.component.scss']
})
export class EventPipelineComponent implements OnInit {
  isInEvent = false;
  constructor() { }

  ngOnInit(): void {
  }

  enteredAnEvent(): void{
    this.isInEvent = true;
  }
}
