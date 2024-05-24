export type ZalandoProduct = {
    id: string;
    url: string;
    name: string;
    sku: string;
    brand: Brand;
    flags: Flag[];
    comingSoon: boolean;
    thumbnail: string;
    images: string[];
    videos: string[];
    reviewsCount: number;
    ratingCount: number;
    rating: number;
    ratingHistogram: RatingHistogram;
    reviews: Review[];
    color: Color;
    priceCurrency: string;
    price: Price;
    sizes: Size[];
    available: boolean;
    sizeAdvice: string;
    navigationTargetGroup: string;
    condition: Condition;
    attributeCategories: AttributeCategory[];
}

export type Brand = {
    name: string;
    uri: string;
}

export type Flag = {
    formatted: string;
    kind: string;
    info: string;
}

export type RatingHistogram = {
    rating1Count: number;
    rating3Count: number;
    rating2Count: number;
    rating4Count: number;
    rating5Count: number;
}

export type Review = {
    authorName: string;
    publishedAt: string;
    text: string;
    rating: number;
}

export type Color = {
    name: string;
    label: string;
}

export type Price = {
    original: number;
    current: number;
    promotional: number;
}

export type Size = {
    size: string;
    sku: string;
    stockStatus: string;
}

export type Attribute = {
    key: string;
    value: string;
}

export type Condition = {
    kind: string;
}

export type AttributeCategory = {
    categoryId: string;
    categoryName: string;
    attributes: Attribute[];
}
