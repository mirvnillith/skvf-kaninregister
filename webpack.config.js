var path = require('path');

module.exports = {
    entry: './src/main/js/App.js',
    mode: 'none',
    devtool: 'source-map',
    cache: true,
    mode: 'development',
    output: {
        path: __dirname,
        filename: './src/main/resources/static/built/bundle.[contenthash].js'
    },
    module: {
        rules: [
            {
                test: path.join(__dirname, '.'),
                exclude: /(node_modules)/,
                use: [{
                    loader: 'babel-loader',
                    options: {
                        presets: ["@babel/preset-env", "@babel/preset-react"],
                        plugins: ['@babel/plugin-transform-runtime']
                    }
                }]
            }
        ]
    }
};