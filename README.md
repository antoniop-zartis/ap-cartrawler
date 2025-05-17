# CarTrawler Assessment

This coding assessment implements a processor for car rental offers, fulfilling the following requirements:

1. Remove duplicate entries based on supplier, description, SIPP code, and fuel policy.
2. Segment results into corporate vs non‑corporate suppliers.
3. Group within each segment by SIPP category: **Mini**, **Economy**, **Compact**, and **Other**.
4. Sort offers by ascending rental cost within each category.

## Tech Stack

* **Java 17** (Gradle toolchain)
* **Spring Boot** (CLI entry point)
* **Gradle**
* **TestNG** & **AssertJ** for testing
* **Lombok** for data classes

## Getting Started

### Run Application

```bash
./gradlew bootRun
```

### Testing

```bash
./gradlew clean test
```

## Assessment Description

The goal is to process a static dataset of car results to produce a clean, ordered list:

* **Deduplicate** entries that share the same supplier, description, SIPP code, and fuel policy, keeping the first occurrence.
* **Prioritize** corporate supplier offers ahead of non‑corporate.
* **Organize** by SIPP category in the fixed sequence: Mini → Economy → Compact → Other.
* **Order** each category by the lowest to highest rental cost.

## Decision Making Process

* **Duplicate Removal:**  
Leveraging `LinkedHashMap<String, CarResult>` keyed by a composite string (`supplier|description|sipp|fuelPolicy`) to both **deduplicate** and **preserve insertion order** for the first-seen entries.

* **Corporate Segmentation:**  
A predefined `Set<String>` of corporate supplier names allows an efficient `filter(...)` to split the dataset.

* **Category Grouping:**  
A simple `Function<CarResult,String>` maps the first character of the SIPP code (`M`, `E`, `C`, others) to category names.  
Grouping is done by streaming over a fixed category list so that empty categories still respect the overall order.

* **Cost Sorting:**  
 We apply `sorted(Comparator.comparingDouble(CarResult::getRentalCost))` within each category to ensure ascending cost order.

* **Final Assembly:**  
By concatenating the sorted streams for corporate and non‑corporate, we guarantee the overall ordering of the final output.
