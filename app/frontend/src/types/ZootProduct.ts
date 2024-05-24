export type ZootProduct = {
    id: string;
    url: string;
    name: string;
    priceCurrency: string;
    currentBestPrice: Price;
    originalPrice: Price;
    saleCode: string;
    thumbnail: string;
    images: string[];
    brand: Brand;
    breadcrumbs: Breadcrumb[];
    description: string;
    attributes: Attribute[];
    sizes: Size[];
    available: boolean;
}

export type Price = {
    value: number;
    formattedPrice: string;
}

export type Brand = {
    link: string;
    logo: string;
}

export type Breadcrumb = {
    text: string;
    url: string;
}

export type Attribute = {
    key: string;
    value: string;
}

export type Size = {
    size: string;
    available: boolean;
}
