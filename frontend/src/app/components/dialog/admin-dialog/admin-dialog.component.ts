import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

export interface AdminDialogData {
  accountType: string;
  name: string;
  email: string;
  banned: boolean;
  banReason: string;
  deactivated: boolean;
  buttonTextOne: string;
  buttonTextTwo: string;
}

@Component({
  selector: 'app-admin-dialog',
  templateUrl: './admin-dialog.component.html',
  styleUrls: ['./admin-dialog.component.scss']
})
export class AdminDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<AdminDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AdminDialogData,
  ) {
  }

  onCloseClick() {
    this.dialogRef.close();
  }

}
