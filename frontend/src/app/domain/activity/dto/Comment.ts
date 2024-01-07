import { User } from "../../user/dto/user";

export interface CommentDTO {
  id?: number,
  text: string,
  author?: User
  date?: number[]
}
