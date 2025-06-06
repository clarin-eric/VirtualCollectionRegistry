# Changelog

## [[1.6.15](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.15)] - 2024-12-13
- Bug Fixes:
  - Fix issue in OAIProvider causing a "no session" error
    
## [[1.6.14](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.14)] - 2024-11-21
- Enhancements:
  - Update of major dependencies including jakarta EE support: wicket, spring, jersey

- Bug Fixes:
  - Unified strategy to get the session in order to fix #231
  - Indefinite collection validaition, fix for #230
  - Fallback to default extensional type if type cannot be derived from page parameters
  - Fixed issue causing collection publication to always fail
  - Fixed some icons that started to fail because of all the dependency updates

## [[1.6.13](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.13-rc1)] - 2023-01-16
- Enhancements:
  - Local docker setup for local development was improved.

## [[1.6.12](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.12)] - 2022-10-21
- Bug fixes:
  - Removal of collections results in foreign key violations from the maintenance thread (#206)
  - Form persistence issue when merging to the same collection twice (#205)
  
## [[1.6.11](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.11)] - 2022-03-31
- Bug fixes:
  - Improved thread based implementation of background reference validation
  - Improved rendering of background validation jobs
 
## [[1.6.10](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.10)] - 2022-03-22
- Enhancements:
  - Added CMDI Explorer support for downloading collections
  - Updated switchboard integration with popup based solution

## [[1.6.9](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.9)] - 2022-01-31
- Bug fixes:
  - Include patch for issue where publishing a collection results in error: There is no application attached to the current thread

## [[1.6.8](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.8)] - 2022-01-31
- Bug fixes:
  - Switch from properties to xml based config for log4j

## [[1.6.7](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.7)] - 2022-01-10
- Bug fixes:
  - Updated to Log4j 2.17.1, resolving log4shell vulnerability

## [[1.6.6](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.6)] - 2021-12-16
- Bug fixes:
  - Updated to Log4j 2.16.0

## [[1.6.5](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.5)] - 2021-12-14
- Bug fixes:
  - Monkey patched log4shell vulnerability

## [[1.6.4](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.4)] - 2021-07-20
- Enhancements:
  - Improved sorting functionality for authors and resources when creating a collection (#159)

## [[1.6.3](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.632)] - 2021-07-13
- Bug fixes:
  - Fixed #156 (Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect))

## [[1.6.2](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.2)] - 2021-07-06
- Enhancements:
  - API Key page improved (#148)
  - Supports JSON LD schema.org metadata in collection details page head section (#150)
  - Supports content negotiation on collection detail pages (#140)
  - Creators website is now rendered as a link (#149)

- Bug fixes:
  - Handle case where no referrer header is available on collection submission. (#151)
  - OpenAPI support improvements

## [[1.6.1](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.1)] - 2021-06-01
- Bug fixes:
  - Solved issue with database connection leak on admin page (#146)
  - Solved issues where all public collection detail pages required authentication (#147)

## [[1.6.0](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.6.0)] - 2021-5-31
- Enhancements:
  - Added OpenAPI support for the REST API (#110)
  - Added option to generate API tokens and use these tokens to authenticate with the REST API (#82)
  - Added option to submit query URI for extensional collections submitted via third part data catalogues (#108)
  - Store referer domain with collections submitted via third party data catalogues (#103)
  - Allow for merging into an existing collection when submitting a collection via a third part data catalogue (#106, #1)
  - Improved admin page

- Bug fixes:
  - Citation popup throws error when closing (#143)
  - Failed to save collection (#142)
  - Failed to remove author after submitting a collection from the VLO (#141)
  - When saving or cancelling a collection submitted from an external application, the cached data is not cleared (#138)
  - Encoding issues when submitting DOIs (#139)

## [[1.5.0](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.5.0)] - 2021-01-18
- Enhancements:
  - Complete rework of create/edit collection form
  - DataCite DOI integration
  - Includes splitting creator names into family name and given name
  - Various other UI changes/improvements
- Bug fixes:
  - Snyk high severity security fixes

## [[1.4.1](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.4.1)] - 2020-10-05
- Enhancements:
  - Better handling if an empty handle infix is configured.
- Bug Fixes:
  - Fixes for #92, #98, #99.
  - Updated dependencies to resolve reported GitHub and snyk.io vulnerabilities.
  - Included doc directory to fix missing Protocol.txt.
  - Included database update to change table and column encodings to utf8mb4_general_ci.

## [[1.4.0](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.4.1)] - 2020-07-09
- Enhancements:
  - Added funding section to the about page
  - Improved the external application collection submit endpoint to support more metadata for references
- Bug Fixes:
  - Fixed encoding issues with input field when creating a collection

## [[1.3.0](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.3.0)]  - 2019-04-08

## [[1.2.0](https://github.com/clarin-eric/VirtualCollectionRegistry/releases/tag/1.2.0)]  -  2019-02-25

For older releases and pre-releases (beta, alpha and release candidates), see https://github.com/clarin-eric/VirtualCollectionRegistry/releases
