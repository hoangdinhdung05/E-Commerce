import { RoleType } from "src/app/utils/RoleType";

export interface AdminCreateUserRequest {
    username: string;
    email: string;
    password: string;
    roles?: RoleType[]; // Optional
}