module.exports = {
    rootDir: ".",
    moduleFileExtensions: ["js", "jsx"],
    moduleDirectories: ["node_modules"],
    modulePaths: [
        "<rootDir>/src"
    ],
    collectCoverage: true,
    collectCoverageFrom: ['src/**/*.{js,jsx}'],
    coverageDirectory: 'coverage',
    testEnvironment: 'jsdom',
    setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
    globalSetup: "<rootDir>/jest.global-setup.jsx"
}
