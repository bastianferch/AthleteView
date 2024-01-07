import { Component, Input, OnInit } from '@angular/core';
import { ActivityService } from "../../service/activity.service";
import { Activity } from "../../dto/Activity";
import { CommentDTO } from "../../dto/Comment";
import { SnackbarService } from "../../../../common/service/snackbar.service";
import { DateParsing } from "../../../../common/util/parsing/date-parsing";
import { UserService } from "../../../user/service/UserService";
import { User } from "../../../user/dto/user";
import { FormControl } from "@angular/forms";

@Component({
  selector: 'app-comments',
  templateUrl: './comments.component.html',
  styleUrls: ['./comments.component.scss'],
})
export class CommentsComponent implements OnInit {

  @Input() activity: Activity;

  private loggedInUser: User = null
  protected commentList: CommentDTO[] = []
  protected commentFormControl = new FormControl()

  constructor(
    private activityService: ActivityService,
    private userService: UserService,
    private snackbarService: SnackbarService,
    private dateParser: DateParsing,
  ) {}

  ngOnInit() {
    // get comments of this activity
    this.commentList = this.activity.comments;
    this.sortComments()

    // get currently logged in user
    this.userService.get().subscribe({
      next: (user) => this.loggedInUser = user,
    })
  }

  commentActivity() {
    if (this.commentFormControl.value === null || this.commentFormControl.value === "") {
      this.commentFormControl.reset();
      return
    }
    const commentText: string = this.commentFormControl.value.trim()
    if (commentText === null || commentText === "") {
      this.commentFormControl.reset();
      return
    }

    if (commentText.length > 1000) {
      this.commentFormControl.setErrors({ invalid: true, message: "Message must not be longer than 1000 characters" })
      return
    }

    this.activityService.commentActivity(this.activity.id, commentText).subscribe({
      next: (newComment) => {
        // could be that sanitization removed everything. Don't add empty comments
        if (newComment.text.trim() === "") {
          return
        }
        this.commentList.push(newComment)
        this.sortComments()
      },
      error: (error) => {
        this.snackbarService.openSnackBar(error.error.message)
      },
    })
    this.commentFormControl.reset();
  }

  getDateOfComment(comment: CommentDTO) {
    return this.dateParser.parseNumbersIntoDate(comment.date)
  }

  getDateString(date: Date) {
    return this.dateParser.getDateAwareString(date)
  }

  isFromLoggedInUser(comment: CommentDTO) {
    if (this.loggedInUser === null) return false;
    return comment.author.id === this.loggedInUser.id
  }

  sortComments() {
    this.commentList.sort((a, b) => {
      const dateA = this.getDateOfComment(a)
      const dateB = this.getDateOfComment(b)
      return dateA.valueOf() - dateB.valueOf()
    })
  }
}
