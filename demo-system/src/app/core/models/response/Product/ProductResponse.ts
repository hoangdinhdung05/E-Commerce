import { CategoryResponse } from "../Category/CategoryResponse";

export interface ProductResponse {
    id: number;
    name: string;
    description?: string;
    price: number;
    quantity: number;
    productImageUrl?: string;
    category: CategoryResponse | null;
}