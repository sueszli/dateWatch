import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

export interface InputDialogData {
  title: string;
  text: string;
  label: string;
  enteredString?: string;
  cancelButton: string;
  enterButton: string;
}

@Component({
  selector: 'app-dialog-input-dialog',
  templateUrl: 'input-dialog.component.html',
})
export class InputDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<InputDialogData>,
    @Inject(MAT_DIALOG_DATA) public data: InputDialogData,
  ) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }
}
