import { Product } from './Product';

export type SimilarProductResponse = {
    distance: number;
    normalizedHammingDistance: number;
    product: Product;
};
