import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {PostStatisticsDto} from '../../../dtos/post-statistics-dto';

@Component({
    selector: 'app-post-statistics-dialog',
    templateUrl: './post-statistics-dialog.component.html',
    styleUrls: ['./post-statistics-dialog.component.scss']
})
export class PostStatisticsDialogComponent {

    constructor(
        public dialogRef: MatDialogRef<PostStatisticsDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: PostStatisticsDto,
    ) {
    }

}
