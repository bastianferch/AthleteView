import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {Component, inject} from '@angular/core';
import {MatChipEditedEvent, MatChipInputEvent} from '@angular/material/chips';
import {LiveAnnouncer} from '@angular/cdk/a11y';
import {MatDialogRef} from '@angular/material/dialog';
import {UserService} from '../user/service/UserService';
import {SnackbarService} from '../../common/service/snackbar.service';


@Component({
  selector: 'app-invite-dialog',
  templateUrl: './invite-dialog.component.html',
  styleUrls: ['./invite-dialog.component.scss']
})
export class InviteDialogComponent {
  addOnBlur = true;
  readonly separatorKeysCodes = [ENTER, COMMA] as const;
  emails: string[] = [];

  announcer = inject(LiveAnnouncer);

  constructor(private dialogRef: MatDialogRef<InviteDialogComponent>, private userService: UserService,
              private snackbarService: SnackbarService) {
  }

  add(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();

    // Add our fruit
    if (value) {
      this.emails.push(value);
    }

    // Clear the input value
    event.chipInput!.clear();
  }

  remove(value: string): void {
    const index = this.emails.indexOf(value);

    if (index >= 0) {
      this.emails.splice(index, 1);

      this.announcer.announce(`Removed ${value}`);
    }
  }

  edit(value: string, event: MatChipEditedEvent) {
    const val = event.value.trim();

    // Remove fruit if it no longer has a name
    if (!val) {
      this.remove(value);
      return;
    }

    // Edit existing fruit
    const index = this.emails.indexOf(value);
    if (index >= 0) {
      this.emails[index] = val;
    }
  }

  sendInvitations() {
    this.userService.sendInvitations(this.emails).subscribe(
      () => {
        this.snackbarService.openSnackBar('Invitations sent')
        this.dialogRef.close(false);
      },
      (error) => {
        this.snackbarService.openSnackBar('Failed to send invitations with the following error ' + error?.error?.message)
        this.dialogRef.close(false);
      },
    );


  }

  close() {
    this.dialogRef.close(false);
  }
}

