const r = {
  Comment: 'An example of the Amazon States Language using a choice state.',
  QueryLanguage: 'JSONata',
  StartAt: 'FirstState',
  States: {
    FirstState: {
      Type: 'Task',
      Assign: {
        'foo': '{% $states.input.foo_input %}'
      },
      Resource: 'arn:aws:lambda:us-east-1:123456789012:function:FUNCTION_NAME',
      Next: 'ChoiceState'
    },
    ChoiceState: {
      Type: 'Choice',
      Default: 'DefaultState',
      Choices: [
        {
          Next: 'FirstMatchState',
          Condition: '{% $foo = 1 %}'
        },
        {
          Next: 'SecondMatchState',
          Condition: '{% $foo = 2 %}'
        }
      ]
    },
    FirstMatchState: {
      Type: 'Task',
      Resource: 'arn:aws:lambda:us-east-1:123456789012:function:OnFirstMatch',
      Next: 'NextState'
    },

    SecondMatchState: {
      Type: 'Task',
      Resource: 'arn:aws:lambda:us-east-1:123456789012:function:OnSecondMatch',
      Next: 'NextState'
    },

    DefaultState: {
      Type: 'Fail',
      Error: 'DefaultStateError',
      Cause: 'No Matches!'
    },

    NextState: {
      Type: 'Task',
      Resource: 'arn:aws:lambda:us-east-1:123456789012:function:FUNCTION_NAME',
      End: true
    }
  }
};
