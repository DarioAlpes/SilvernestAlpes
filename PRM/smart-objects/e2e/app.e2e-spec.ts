import { SmartObjectsPage } from './app.po';

describe('smart-objects App', function() {
  let page: SmartObjectsPage;

  beforeEach(() => {
    page = new SmartObjectsPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
