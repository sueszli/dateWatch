import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-delete-account-dialog',
  templateUrl: './delete-account-dialog.component.html',
  styleUrls: ['./delete-account-dialog.component.scss']
})
export class DeleteAccountDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<DeleteAccountDialogComponent>
  ) {
  }

  setFalse() {
    this.dialogRef.close(false);
  }

  setTrue() {
    this.dialogRef.close(true);
  }
}
