import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
  selector: 'app-deactivate-account-dialog',
  templateUrl: './deactivate-account-dialog.component.html',
  styleUrls: ['./deactivate-account-dialog.component.scss']
})
export class DeactivateAccountDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<DeactivateAccountDialogComponent>
  ) {
  }

  setFalse() {
    this.dialogRef.close(false);
  }

  setTrue() {
    this.dialogRef.close(true);
  }
}
