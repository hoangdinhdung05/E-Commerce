import { RoleType } from "src/app/utils/RoleType";
import { UserStatus } from "src/app/utils/UserStatus";

export interface UpdateUserRequest {
  firstName?: string;
  lastName?: string;
  status?: UserStatus;
  verifyEmail?: boolean;
  roles?: RoleType[];
}
