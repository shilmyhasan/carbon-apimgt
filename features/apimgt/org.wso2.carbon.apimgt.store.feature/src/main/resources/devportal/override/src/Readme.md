# Override root folder
If you want to override a certain React Component / File from source/src/ folder, this is the correct place to do it.
You do not have to copy the entire directory, only copy the desired file/files.

#### Example
Following will override the API Documentation component and Overview components.
```sh
override
└── src
    ├── Readme.txt
    └── app
        └── components
            └── Apis
                └── Details
                    ├── Documents
                    │   └── Documentation.jsx
                    └── Overview.jsx
```
### Adding new files to the override folder¶

override
└── src
    ├── Readme.txt
    └── app
        └── components
            └── Apis
                └── Details
                    ├── Documents
                    │   └── Documentation.jsx
                    └── Overview.jsx
                    └── NewFile.jsx
                    

If we try to import the NewFile.jsx from Overview.jsx as follows it will give a compilation error.

```sh
import NewFile from './NewFile.jsx';
```

The correct way is to add the AppOverride prefix to the import and provide the full path relative to the override folder.

```sh
import NewFile from 'AppOverride/src/app/components/Apis/Details/NewFile.jsx';
```

### Development

When you are doing active development, the watch mode is working with the overriden files. But adding new files and directories will not triger a new webpack build.

```sh
npm run build:dev
```
