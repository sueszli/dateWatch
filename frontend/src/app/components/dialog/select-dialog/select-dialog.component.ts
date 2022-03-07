import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';


export interface SelectDialogData {
  title: string;
  text: string;
  label: string;
  options: string[];
  enteredString?: string;
}

@Component({
  selector: 'app-dialog-select-dialog',
  templateUrl: 'select-dialog.component.html',
})
export class SelectDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<SelectDialogData>,
    @Inject(MAT_DIALOG_DATA) public data: SelectDialogData,
  ) {}

  onNoClick(): void {
    this.dialogRef.close();
  }
}
